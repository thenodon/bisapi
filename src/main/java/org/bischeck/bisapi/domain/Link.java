package org.bischeck.bisapi.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class Link implements Comparable<Link> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Link.class);
    private String href = null;
    private String rel = null;
    private Optional<String> mediaType = null;
    private Optional<String> method = Optional.of("\"GET\"");
    private Optional<String> title = null;
    private Optional<String> description = null;

    public Link(String href, String rel, String title) {
        this.href = makeJsonAttr(href);
        this.rel = makeJsonAttr(rel);
        this.title = Optional.fromNullable(makeJsonAttr(title));
    }

    private String makeJsonAttr(String attr) {
        if (attr == null) {
            return null;
        }
        StringBuilder strbu = new StringBuilder();
        strbu.append("\"").append(attr).append("\"");
        return strbu.toString();
    }

    @Override
    public String toString() {

        StringBuilder strbu = new StringBuilder();

        strbu.append('{');

        if (title.isPresent()) {
            strbu.append("\"title\":").append(title.get()).append(',');
        }

        if (method.isPresent()) {
            strbu.append("\"method\":").append(method.get()).append(',');
        }
       
        strbu.append("\"rel\":").append(rel).append(',').append("\"href\":")
                .append(htmlEncode(href))
                .append('}');

        return strbu.toString();
    }

    private String htmlEncode(String href) {
        String key = href.substring(0, href.indexOf('/') + 1);
        String toEncode = href.substring(href.indexOf('/') + 1,
                href.length() - 1);
        
        String returnKey = null;
        try {
            String encoded = URLEncoder.encode(toEncode, "UTF-8");
            returnKey = key + encoded + "\"";
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Encoding of string {} failed- Returning the orignal",
                    href, e);
            returnKey = href;
        }
        return returnKey;
    }

    @Override
    public int compareTo(Link o) {
        return this.href.compareTo(o.href);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result
                + ((mediaType == null) ? 0 : mediaType.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((rel == null) ? 0 : rel.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        Link other = (Link) obj;
        
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        
        if (mediaType == null) {
            if (other.mediaType != null) {
                return false;
            }
        } else if (!mediaType.equals(other.mediaType)) {
            return false;
        }
        
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        
        if (rel == null) {
            if (other.rel != null) {
                return false;
            }
        } else if (!rel.equals(other.rel)) {
            return false;
        }
        
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        
        return true;
    }

}
