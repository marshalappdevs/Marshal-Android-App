package com.basmapp.marshal.localdb;

import java.lang.reflect.Method;

public class Validator {

    public static final int VALID_FLAG = 999;

    public static final int TYPE_NUMERIC_LONG_POSITIVE = 12;
    public static final int TYPE_STRING = 13;
    public static final int TYPE_FOREIGN_KEY_ENTITY = 14;
    public static final int TYPE_DATE = 15;

    public static final String ERROR_CANNOT_BE_NULL         = "field must contain a value";
    public static final String ERROR_WRONG_VALIDATION_CODE  = "wrong validation code. Call to the programmer";
    public static final String ERROR_NOT_NUMERIC            = "value must be numeric";
    public static final String ERROR_NOT_POSOTIVE           = "number must be positive";

    private static final String NULL_STRING = "";

    public static void setAttribute(Object setterOwner, Method setter,
                                    String fieldName, boolean isNullable,
                                    Object value, int validationType) throws Exception {

        if (!isNullable){
            if(value == null || value.toString().equals(NULL_STRING)) {
                throw new Exception(fieldName + " " + ERROR_CANNOT_BE_NULL);
            }
            else {
                try {
                    isValid(setterOwner, setter, fieldName,value,validationType);
                }
                catch (Exception e){
                    throw e;
                }
            }
        }
        else {
            try {
                isValid(setterOwner, setter, fieldName,value,validationType);
            }
            catch (Exception e){
                throw e;
            }
        }
    }

    private static void isValid(Object setterOwner, Method setter,
                                String fieldName, Object value,
                                int validationType) throws Exception {

        setter.setAccessible(true);

        if (value == null) {
            setter.invoke(setterOwner, value);
        }
        else {
            switch (validationType) {
                case TYPE_NUMERIC_LONG_POSITIVE :
                    try{
                        long numericValue = Long.valueOf((String)value);
                        if (numericValue < 1) {
                            throw new Exception(ERROR_NOT_POSOTIVE);
                        }
                        else {
                            setter.invoke(setterOwner,numericValue);
                        }
                    }
                    catch (NumberFormatException e){
                        throw new Exception(fieldName + " " + ERROR_NOT_NUMERIC);
                    }
                    break;
                case TYPE_STRING :
                    setter.invoke(setterOwner, value.toString());
                    break;
                case TYPE_FOREIGN_KEY_ENTITY :
                    setter.invoke(setterOwner, value);
                    break;
                case TYPE_DATE :
                    setter.invoke(setterOwner, value);
                    break;
                default :
                    throw new Exception(ERROR_WRONG_VALIDATION_CODE);
            }
        }
    }
}
