package com.oneandone.network.snmpman.configuration.modifier;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.oneandone.network.snmpman.configuration.type.ModifierProperties;
import lombok.Getter;
import org.snmp4j.smi.UnsignedInteger32;

/**
 * This modifier has all utility methods to construct for unsigned integer variable modifiers.
 *
 * @author Johann Böhler
 */
abstract class AbstractIntegerModifier<T extends UnsignedInteger32> implements VariableModifier<T> {

    /** The minimum allowed number for the resulting modified variable. */
    @Getter private Long minimum;

    /** The maximum allowed number for the resulting modified variable. */
    @Getter private Long maximum;

    /** The minimal step by which a variable will be incremented. */
    @Getter private Long minimumStep;

    /** The maximal step by which a variable will be incremented. */
    @Getter private Long maximumStep;

    @Override
    public void init(final ModifierProperties properties) {
        try {
            this.minimum = Optional.fromNullable(properties.getLong("minimum")).or(0L);
            this.maximum = Optional.fromNullable(properties.getLong("maximum")).or(UnsignedInteger.MAX_VALUE.longValue());

            this.minimumStep = Optional.fromNullable(properties.getLong("minimumStep")).or(0L);
            this.maximumStep = Optional.fromNullable(properties.getLong("maximumStep")).or(1L);

            Preconditions.checkArgument(minimum >= 0, "minimum should not be negative");
            Preconditions.checkArgument(maximum >= 0, "maximum should not be negative");

            Preconditions.checkArgument(minimum <= UnsignedInteger.MAX_VALUE.longValue(), "minimum should not exceed 2^32-1 (4294967295 decimal)");
            Preconditions.checkArgument(maximum <= UnsignedInteger.MAX_VALUE.longValue(), "maximum should not exceed 2^32-1 (4294967295 decimal)");
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("one of the parameters exceeds the legal long value range", e);
        }
    }

    protected abstract T cast(long value);
    
    @Override
    public final T modify(final T variable) {
        long currentValue = variable.getValue();
        if (currentValue < minimum || currentValue > maximum) {
            currentValue = minimum;
        }
        long step = (Math.round(Math.random() * (maximumStep - minimumStep)) + minimumStep);

        long stepUntilMaximum = maximum - currentValue;
        long newValue;
        if (Math.abs(step) > Math.abs(stepUntilMaximum)) {
            newValue = minimum + (step - stepUntilMaximum - 1);
        } else {
            newValue = currentValue + step;
        }

        if (newValue < minimum) {
            newValue = minimum;
        } else if (newValue > maximum) {
            newValue = maximum;
        }
        
        return cast(newValue);
    }
}
