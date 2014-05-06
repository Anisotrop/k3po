/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.robot.lang.ast;

public class AstConnectedNode extends AstEventNode {

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) throws Exception {
        return visitor.visit(this, parameter);
    }

    @Override
    public int hashCode() {
        return hashTo();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof AstConnectedNode) && equalTo((AstConnectedNode) obj));
    }

    @Override
    protected void formatNode(StringBuilder sb) {
        super.formatNode(sb);
        sb.append("connected\n");
    }
}
