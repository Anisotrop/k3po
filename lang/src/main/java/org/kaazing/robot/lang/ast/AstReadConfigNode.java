/*
 * Copyright (c) 2014 "Kaazing Corporation," (www.kaazing.com)
 *
 * This file is part of Robot.
 *
 * Robot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kaazing.robot.lang.ast;

import static java.lang.String.format;
import static org.kaazing.robot.lang.ast.util.AstUtil.equivalent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kaazing.robot.lang.ast.matcher.AstValueMatcher;
import org.kaazing.robot.lang.ast.value.AstValue;

public class AstReadConfigNode extends AstEventNode {

    private String type;
    private Map<String, AstValue> valuesByName;
    private Map<String, AstValueMatcher> matchersByName;

    public AstReadConfigNode() {
        this.valuesByName = new HashMap<String, AstValue>();
        this.matchersByName = new HashMap<String, AstValueMatcher>();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setValue(String name, AstValue value) {
        valuesByName.put(name, value);
    }

    public AstValue getValue(String name) {
        return valuesByName.get(name);
    }

    public Collection<AstValueMatcher> getMatchers() {
        return matchersByName.values();
    }

    public AstValueMatcher getMatcher(String name) {
        return matchersByName.get(name);
    }

    public void setMatcher(String name, AstValueMatcher matcher) {
        matchersByName.put(name, matcher);
    }

    public void addMatcher(AstValueMatcher matcher) {
        matchersByName.put(format("matcher#%d", matchersByName.size()), matcher);
    }

    public void addMatchers(Collection<AstValueMatcher> matchers) {
        for (AstValueMatcher matcher : matchers) {
            matchersByName.put(format("matcher#%d", matchersByName.size()), matcher);
        }
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) throws Exception {
        return visitor.visit(this, parameter);
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashTo();

        if (type != null) {
            hashCode <<= 4;
            hashCode ^= type.hashCode();
        }
        if (valuesByName != null) {
            hashCode <<= 4;
            hashCode ^= valuesByName.hashCode();
        }
        if (matchersByName != null) {
            hashCode <<= 4;
            hashCode ^= matchersByName.hashCode();
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof AstReadConfigNode) && equals((AstReadConfigNode) obj));
    }

    protected boolean equals(AstReadConfigNode that) {
        return super.equalTo(that) &&
                equivalent(this.type, that.type) &&
                equivalent(this.valuesByName, that.valuesByName) &&
                equivalent(this.matchersByName, that.matchersByName);
    }

    @Override
    protected void formatNode(StringBuilder sb) {
        super.formatNode(sb);
        sb.append("read ").append(type);
        for (AstValue value : valuesByName.values()) {
            sb.append(' ').append(value);
        }
        for (AstValueMatcher matcher : matchersByName.values()) {
            sb.append(' ').append(matcher);
        }
        sb.append('\n');
    }
}
