package org.bischeck.bisapi.rest;

public class LabelText {

    public static final String ALL_KEYS = "*";

    public static final String STATE_KEY = "state/";
    public static final String STATE_TITLE = "state";

    public static final String NOTIFICATION_KEY = "notification/";
    public static final String NOTIFICATION_TITLE = "notification";

    public static final String METRIC_KEY = "metric/";
    public static final String METRIC_TITLE = "metric";

    static final String KEY = "\"key\":\"";
    static final String SIZE = "\"size\":";
    static final String LINKS = "\"links\" : [";

    static final String REL_SELF = "self";
    static final String REL_RELATED = "related";
    static final String REL_ROOT = "root";
    static final String REL_STATE = "state";
    static final String REL_NOTIFICATION = "notification";
    static final String REL_METRIC = "metric";
    static final String REL_AGGREGATION = "aggregation";

    private LabelText() {

    }
}
