package org.bischeck.bisapi.domain;

import java.util.Set;

public interface ResponseInf {

    Set<Link> getLinks();

    void setLinks(Set<Link> links);

    Set<String> getResult();

    void setResult(Set<String> res);

    Integer getCount();

    Long getProcessingTime();

    void setProcessingTime(Long responseTime);

}
