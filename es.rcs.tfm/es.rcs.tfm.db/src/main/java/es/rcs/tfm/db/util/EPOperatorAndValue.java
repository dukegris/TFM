/*******************************************************************************
 * Copyright (c) 2018 @gt_tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package es.rcs.tfm.db.util;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Utility class that extracts EPOperator and Value using the list of supplied
 * {@link EPOperator}
 */
public class EPOperatorAndValue {

    private EPOperator operator;
    private String value;

    /**
     * Constructor
     * <p>
     * Leans towards selecting picking case-insensitive Operation.
     *
     * @param input           Input String (search parameter's, usually an attribute, value)
     * @param in_operators    Input list of operators to be used to check for operator
     * @param defaultOperator Default EPOperator if the supplied input string doesn't have any
     *                        operator
     */
    public EPOperatorAndValue(final String input, final Collection<EPOperator> in_operators,
                            final EPOperator defaultOperator) {
        Validate.isTrue(StringUtils.isNotEmpty(input), "Input string cannot be blank");
        Validate.isTrue(CollectionUtils.isNotEmpty(in_operators), "Input operators must not be empty");
        this.operator = null;
        this.value = StringUtils.trim(input);
        for (final EPOperator op : in_operators) {
            if (EP.isOperator(op, input)) {
                this.operator = op;
                this.value = StringUtils.substringBeforeLast(
                    StringUtils.substringAfter(
                		input,
                        op.toString() + EP.OPERATOR_VALUE_DELIMITER_PREFIX),
                	EP.OPERATOR_VALUE_DELIMITER_SUFFIX);

                break;
            }
        }

        if (this.operator == null) {
            this.operator = defaultOperator;
        }
    }

    /**
     * @return EPOperator extracted from supplied value or else provided default operator
     */
    public EPOperator getOperator() {
        return operator;
    }

    /**
     * @return the String value from supplied value after stripping first operator if it was an operator wrapped value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        		.append("operator", operator)
                .append("value", value)
                .toString();
    }
}
