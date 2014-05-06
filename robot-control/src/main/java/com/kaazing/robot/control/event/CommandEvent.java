/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.robot.control.event;

public abstract class CommandEvent {

    public enum Kind {
        PREPARED, STARTED, FINISHED, ERROR
    }

    private String name;

    public abstract Kind getKind();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected int hashTo() {
        return name != null ? name.hashCode() : 0;
    }

    protected boolean equalTo(CommandEvent that) {
        return this.name == that.name || this.name != null && this.name.equals(that.name);
    }
}
