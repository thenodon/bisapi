package org.bischeck.bisapi.rest;

import java.util.Date;

import org.eclipse.jetty.http.HttpStatus;

public class ApiError {

    public enum ErrorRef {
        FIELD {
            @Override
            public String code() {
                return "API-001";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Filter field does not exists";
            }
        },
        PARSING_JSON {
            @Override
            public String code() {
                return "API-002";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Parsing result json failed";
            }
        },
        FROMTO {
            @Override
            public String code() {
                return "API-003";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "From and to parameters must be >= 0";
            }
        },
        QUERY_STATEMENT {
            @Override
            public String code() {
                return "API-004";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Query statement is not correct";
            }
        },
        QUERY_STATEMENT_FIELD {
            @Override
            public String code() {
                return "API-005";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Query statement include a none valid field";
            }
        },
        KEY_DO_NOT_EXISTS {
            @Override
            public String code() {
                return "API-006";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Key does not exists";
            }
        },
        REDIS_DATA_STRUCTURE_NOT_SUPPORTED {
            @Override
            public String code() {
                return "API-007";
            }

            @Override
            public Integer status() {
                return HttpStatus.BAD_REQUEST_400;
            }

            @Override
            public String description() {
                return "Key does not belong to a supported Redis structure";
            }
        },
        ENCODING_NOT_SUPPORTED {
            @Override
            public String code() {
                return "API-008";
            }

            @Override
            public Integer status() {
                return HttpStatus.INTERNAL_SERVER_ERROR_500;
            }

            @Override
            public String description() {
                return "Encoding is not supported";
            }
        };

        public abstract String code();

        public abstract Integer status();

        public abstract String description();

    }

    private final ErrorRef errorRef;
    private final String cause;
    private final long timestamp;

    public ApiError(ErrorRef errorRef, String cause) {
        this.errorRef = errorRef;
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }

    public Integer getStatus() {
        return errorRef.status();
    }

    @Override
    public String toString() {
        StringBuilder strbu = new StringBuilder();
        strbu.append('{').append("\"errorTime\":\"")
                .append((new Date(timestamp)).toString())
                .append("\",\"errorCode\":\"").append(errorRef.code())
                .append("\",\"description\":\"").append(errorRef.description())
                .append("\",").append("\"cause\":\"").append(cause)
                .append("\",\"status\":").append(errorRef.status()).append('}');
        return strbu.toString();
    }

    public static String defaultError() {
        StringBuilder strbu = new StringBuilder();
        strbu.append('{').append("\"errorTime\":\"")
                .append((new Date(System.currentTimeMillis())).toString())
                .append("\",\"errorCode\":\"").append("Unknown")
                .append("\",\"description\":\"")
                .append("Could not determine error cause").append("\",")
                .append("\"cause\":\"").append("Unknown")
                .append("\",\"status\":").append("500").append('}');
        return strbu.toString();
    }

}
