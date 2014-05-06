/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.robot.lang.parser.v1;

import java.util.List;

@SuppressWarnings("serial")
public class TooManyErrorsException extends ParseException {

    public TooManyErrorsException(String msg) {
        super(msg);
    }

    public TooManyErrorsException(String msg, List<String> errors) {
        this(msg);
        setErrors(errors);
    }
}
