package stparta300.snapi.domain.user.handler;

import stparta300.snapi.global.error.code.status.BaseErrorCode;
import stparta300.snapi.global.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode baseErrorCode) { super(baseErrorCode); }
}