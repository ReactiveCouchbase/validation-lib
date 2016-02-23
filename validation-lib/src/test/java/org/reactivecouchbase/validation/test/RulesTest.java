package org.reactivecouchbase.validation.test;

import org.junit.Test;
import org.reactivecouchbase.validation.Rules;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class RulesTest {

    @Test
    public void testEmail() throws Exception {
        assertThat(Rules.email().validate("john.doe@gmail.com").isSuccess()).isTrue();
    }

    @Test
    public void testUrl() throws Exception {
        assertThat(Rules.url().validate("http://www.google.fr").isSuccess()).isTrue();
    }

    @Test
    public void testPhone() throws Exception {
        assertThat(Rules.phone().validate("0506060606").isSuccess()).isTrue();
    }

    @Test
    public void testPatternP() throws Exception {
        assertThat(Rules.pattern("[A-Z]").validate("A").isSuccess()).isTrue();
        assertThat(Rules.pattern("[A-Z]").validate("AA").isSuccess()).isFalse();
    }

    @Test
    public void testMinI() throws Exception {
        assertThat(Rules.minI(1).validate(10).isSuccess()).isTrue();
        assertThat(Rules.minI(1).validate(0).isSuccess()).isFalse();
    }

    @Test
    public void testMaxI() throws Exception {
        assertThat(Rules.maxI(1).validate(10).isSuccess()).isFalse();
        assertThat(Rules.maxI(1).validate(0).isSuccess()).isTrue();
    }

    @Test
    public void testMinL() throws Exception {
        assertThat(Rules.minL(1L).validate(10L).isSuccess()).isTrue();
        assertThat(Rules.minL(1L).validate(0L).isSuccess()).isFalse();
    }

    @Test
    public void testMaxL() throws Exception {
        assertThat(Rules.maxL(1L).validate(10L).isSuccess()).isFalse();
        assertThat(Rules.maxL(1L).validate(0L).isSuccess()).isTrue();
    }

    @Test
    public void testMinD() throws Exception {
        assertThat(Rules.minD(1.0).validate(10.0).isSuccess()).isTrue();
        assertThat(Rules.minD(1.0).validate(0.0).isSuccess()).isFalse();
    }

    @Test
    public void testMaxD() throws Exception {
        assertThat(Rules.maxD(1.0).validate(10.0).isSuccess()).isFalse();
        assertThat(Rules.maxD(1.0).validate(0.0).isSuccess()).isTrue();
    }

    @Test
    public void testMinS() throws Exception {
        assertThat(Rules.minF(1.0f).validate(10.0f).isSuccess()).isTrue();
        assertThat(Rules.minF(1.0f).validate(0.0f).isSuccess()).isFalse();
    }

    @Test
    public void testMaxS() throws Exception {
        assertThat(Rules.maxF(1.0f).validate(10.0f).isSuccess()).isFalse();
        assertThat(Rules.maxF(1.0f).validate(0.0f).isSuccess()).isTrue();
    }

    @Test
    public void testMinF() throws Exception {
        assertThat(Rules.minF(1.0f).validate(10.0f).isSuccess()).isTrue();
        assertThat(Rules.minF(1.0f).validate(0.0f).isSuccess()).isFalse();
    }

    @Test
    public void testMaxF() throws Exception {
        assertThat(Rules.maxF(1.0f).validate(10.0f).isSuccess()).isFalse();
        assertThat(Rules.maxF(1.0f).validate(0.0f).isSuccess()).isTrue();
    }

    @Test
    public void testMinBD() throws Exception {
        assertThat(Rules.minBD(BigDecimal.ONE).validate(BigDecimal.TEN).isSuccess()).isTrue();
        assertThat(Rules.minBD(BigDecimal.ONE).validate(BigDecimal.ZERO).isSuccess()).isFalse();
    }

    @Test
    public void testMaxBD() throws Exception {
        assertThat(Rules.maxBD(BigDecimal.ONE).validate(BigDecimal.TEN).isSuccess()).isFalse();
        assertThat(Rules.maxBD(BigDecimal.ONE).validate(BigDecimal.ZERO).isSuccess()).isTrue();
    }

    @Test
    public void testMinBI() throws Exception {
        assertThat(Rules.minBI(BigInteger.ONE).validate(BigInteger.TEN).isSuccess()).isTrue();
        assertThat(Rules.minBI(BigInteger.ONE).validate(BigInteger.ZERO).isSuccess()).isFalse();
    }

    @Test
    public void testMaxBI() throws Exception {
        assertThat(Rules.maxBI(BigInteger.ONE).validate(BigInteger.TEN).isSuccess()).isFalse();
        assertThat(Rules.maxBI(BigInteger.ONE).validate(BigInteger.ZERO).isSuccess()).isTrue();
    }

    @Test
    public void testIgnore() throws Exception {
        assertThat(Rules.ignore().validate("anything").isSuccess()).isTrue();
        assertThat(Rules.ignore().validate(123).isSuccess()).isTrue();
        assertThat(Rules.ignore().validate(null).isSuccess()).isTrue();
    }

    @Test
    public void testNotNull() throws Exception {
        assertThat(Rules.notNull().validate(null).isSuccess()).isFalse();
        assertThat(Rules.notNull().validate("").isSuccess()).isTrue();
    }

    @Test
    public void testIsNull() throws Exception {
        assertThat(Rules.isNull().validate(null).isSuccess()).isTrue();
        assertThat(Rules.isNull().validate("").isSuccess()).isFalse();
    }

    @Test
    public void testEqualsTo() throws Exception {
        assertThat(Rules.equalsTo("hello").validate("hello").isSuccess()).isTrue();
        assertThat(Rules.equalsTo("hello").validate("helllo").isSuccess()).isFalse();
    }

    @Test
    public void testNotEmptyStr() throws Exception {
        assertThat(Rules.notEmptyStr().validate("qsd").isSuccess()).isTrue();
        assertThat(Rules.notEmptyStr().validate("").isSuccess()).isFalse();
    }

    @Test
    public void testEmptyStr() throws Exception {
        assertThat(Rules.emptyStr().validate("").isSuccess()).isTrue();
        assertThat(Rules.emptyStr().validate("dsq").isSuccess()).isFalse();
    }

    @Test
    public void testNotEmpty() throws Exception {
        assertThat(Rules.notEmpty().validate(new ArrayList<>()).isSuccess()).isFalse();
        assertThat(Rules.notEmpty().validate(Collections.singletonList("")).isSuccess()).isTrue();
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertThat(Rules.isEmpty().validate(new ArrayList<>()).isSuccess()).isTrue();
        assertThat(Rules.isEmpty().validate(Collections.singletonList("")).isSuccess()).isFalse();
    }

    @Test
    public void testMinLength() throws Exception {
        assertThat(Rules.minLength(3).validate("oooO").isSuccess()).isTrue();
        assertThat(Rules.minLength(3).validate("oo").isSuccess()).isFalse();
    }

    @Test
    public void testMaxLength() throws Exception {
        assertThat(Rules.maxLength(3).validate("oo").isSuccess()).isTrue();
        assertThat(Rules.maxLength(3).validate("ooo").isSuccess()).isTrue();
        assertThat(Rules.maxLength(3).validate("ooOo").isSuccess()).isFalse();
    }

    @Test
    public void testDate() throws Exception {
        assertThat(Rules.date("dd/MM/yyyy").validate("12/12/2012").isSuccess()).isTrue();
    }

    @Test
    public void testDateTime() throws Exception {
        assertThat(Rules.dateTime("dd/MM/yyyy").validate("12/12/2012").isSuccess()).isTrue();
    }

    @Test
    public void testGreaterThan() throws Exception {
        assertThat(Rules.greaterThan(3).validate(4).isSuccess()).isTrue();
        assertThat(Rules.greaterThan(3).validate(2).isSuccess()).isFalse();
    }

    @Test
    public void testLesserThan() throws Exception {
        assertThat(Rules.lesserThan(3).validate(2).isSuccess()).isTrue();
        assertThat(Rules.lesserThan(3).validate(3).isSuccess()).isFalse();
    }


} 
