package com.mizanflow;
import com.mizanflow.common.*; import com.mizanflow.invoice.*; import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class MoneyAndInvoiceDomainTest {
  private Invoice issued(String qty, String price, String vat){
    var inv = new Invoice(); var l = new InvoiceLine(); l.setLabel("Dalle"); l.setQuantity(new BigDecimal(qty)); l.setUnit("M2"); l.setUnitPrice(new BigDecimal(price)); l.setVatRate(new BigDecimal(vat)); inv.addLine(l);
    inv.recomputeTotals(); inv.issue("FAC-2026-0001", LocalDate.of(2026,1,10), LocalDate.of(2026,2,10)); return inv; }
  @Test void totalsUseDecimalArithmetic(){ var inv = issued("10.5","99.99","20"); assertEquals(new BigDecimal("1049.90"), inv.getTotalHt()); assertEquals(new BigDecimal("209.98"), inv.getTotalVat()); assertEquals(new BigDecimal("1259.88"), inv.getTotalTtc()); }
  @Test void discountApplied(){ assertEquals(new BigDecimal("900.00"), Money.lineHt(new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("10"))); }
  @Test void partialPaymentUpdatesBalanceAndStatus(){ var inv = issued("1","1000","20"); inv.applyPayment(new BigDecimal("400"));
    assertEquals(Invoice.Status.PARTIALLY_PAID, inv.getStatus()); assertEquals(new BigDecimal("800.00"), inv.balance()); }
  @Test void fullPaymentOnlyWhenBalanceIsZero(){ var inv = issued("1","1000","20"); inv.applyPayment(new BigDecimal("1199.99")); assertEquals(Invoice.Status.PARTIALLY_PAID, inv.getStatus());
    inv.applyPayment(new BigDecimal("0.01")); assertEquals(Invoice.Status.PAID, inv.getStatus()); }
  @Test void overpaymentRejected(){ var inv = issued("1","1000","20"); assertThrows(Exceptions.BusinessRule.class, () -> inv.applyPayment(new BigDecimal("1200.01"))); }
  @Test void draftInvoiceCannotReceivePayment(){ var inv = new Invoice(); assertThrows(Exceptions.BusinessRule.class, () -> inv.applyPayment(BigDecimal.ONE)); }
  @Test void overdueIsDerivedFromDueDateAndBalance(){ var inv = issued("1","1000","20");
    assertFalse(inv.isOverdue(LocalDate.of(2026,2,10))); assertTrue(inv.isOverdue(LocalDate.of(2026,2,11)));
    inv.applyPayment(new BigDecimal("1200")); assertFalse(inv.isOverdue(LocalDate.of(2026,3,1))); }
  @Test void paidInvoiceCannotBeCancelled(){ var inv = issued("1","1000","20"); inv.applyPayment(BigDecimal.TEN); assertThrows(Exceptions.BusinessRule.class, () -> inv.cancel("erreur")); }
}
