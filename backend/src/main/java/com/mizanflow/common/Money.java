package com.mizanflow.common;
import java.math.*;
/** Tous les calculs financiers : BigDecimal, arrondi HALF_UP à 2 décimales. Jamais de double. */
public final class Money { private Money(){}
  public static final RoundingMode RM = RoundingMode.HALF_UP; public static final BigDecimal HUNDRED = new BigDecimal("100");
  public static BigDecimal round(BigDecimal v){ return v.setScale(2, RM); }
  public static BigDecimal lineHt(BigDecimal qty, BigDecimal unitPrice, BigDecimal discountPct){
    BigDecimal gross = qty.multiply(unitPrice); BigDecimal factor = BigDecimal.ONE.subtract(discountPct.divide(HUNDRED, 6, RM)); return round(gross.multiply(factor)); }
  public static BigDecimal vat(BigDecimal ht, BigDecimal ratePct){ return round(ht.multiply(ratePct).divide(HUNDRED, 6, RM)); }
}
