package org.bischeck.bisapi.domain;

import java.util.Iterator;
import java.util.Set;

public class PostStatus extends ResponseAbstract implements ResponseInf {
    
    private String status;
    private String key;
    private Set<Link> links;
    private Set<String> result;
    private Long responseTime;
    
    public PostStatus(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    
    @Override
    public Set<Link> getLinks() {
        return links;
    }

    @Override
    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    @Override
    public Set<String> getResult() {
        return result;
    }

    @Override
    public void setResult(Set<String> res) {
        this.result = res;
    }

    @Override
    public Integer getCount() {
        if (result == null) {
            return 0;
        }
        return result.size();
    }

    @Override
    public Long getProcessingTime() {
        return responseTime;
    }

    @Override
    public void setProcessingTime(Long responseTime) {
        this.responseTime = responseTime;
    }
    @Override
    public String toString() {
        StringBuilder strbu = new StringBuilder();
        strbu.append("{");

        strbu.append(KEY).append(getKey()).append("\",");
        strbu.append(COUNT).append(getCount()).append(',');
        strbu.append(PROCESSING_TIME).append(getProcessingTime()).append(',');
        strbu.append(RESULT_BEGIN);
        if (result != null && !result.isEmpty()) {
            Iterator<String> iter = result.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (!first) {
                    strbu.append(",");
                } else {
                    first = false;
                }
                strbu.append(iter.next());
            }
        }
        strbu.append(RESULT_END);

        strbu.append(',');

        strbu.append(LINK_BEGIN);
        if (links != null && !links.isEmpty()) {
            Iterator<Link> iter = links.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (!first) {
                    strbu.append(",");
                } else {
                    first = false;
                }
                strbu.append(iter.next().toString());
            }
        }
        strbu.append(LINK_END);

        strbu.append('}');
        return strbu.toString();
        
    }
}
