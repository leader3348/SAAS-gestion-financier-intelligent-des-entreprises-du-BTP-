package com.mizanflow;
import com.mizanflow.common.Exceptions; import com.mizanflow.worklog.WorkLog; import com.mizanflow.worklog.WorkLog.Status; import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class WorkLogWorkflowTest {
  private WorkLog wl(){ var w = new WorkLog(); w.setQuantity(new BigDecimal("10")); return w; }
  @Test void happyPath(){ var w = wl(); w.transitionTo(Status.SUBMITTED); w.transitionTo(Status.APPROVED); w.transitionTo(Status.INVOICED); assertEquals(Status.INVOICED, w.getStatus()); }
  @Test void rejectedWorkCannotBeInvoiced(){ var w = wl(); w.transitionTo(Status.SUBMITTED); w.transitionTo(Status.REJECTED); assertThrows(Exceptions.BusinessRule.class, () -> w.transitionTo(Status.INVOICED)); }
  @Test void draftCannotBeApprovedDirectly(){ assertThrows(Exceptions.BusinessRule.class, () -> wl().transitionTo(Status.APPROVED)); }
  @Test void invoicedIsTerminal(){ var w = wl(); w.transitionTo(Status.SUBMITTED); w.transitionTo(Status.APPROVED); w.transitionTo(Status.INVOICED); assertThrows(Exceptions.BusinessRule.class, () -> w.transitionTo(Status.SUBMITTED)); }
  @Test void rejectedCanBeCorrectedAndResubmitted(){ var w = wl(); w.transitionTo(Status.SUBMITTED); w.transitionTo(Status.REJECTED); w.transitionTo(Status.SUBMITTED); assertEquals(Status.SUBMITTED, w.getStatus()); }
}
