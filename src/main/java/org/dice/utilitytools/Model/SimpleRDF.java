package org.dice.utilitytools.Model;

import java.util.Objects;

public class SimpleRDF {
    private String Id;
    private String Subject;
    private String Predicate;
    private String Object;
    private Boolean correctness;

    public SimpleRDF(){}

    public SimpleRDF(String id, String subject, String predicate, String object) {
        Id = id;
        Subject = subject;
        Predicate = predicate;
        Object = object;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String sobject) {
        Subject = sobject;
    }

    public String getPredicate() {
        return Predicate;
    }

    public void setPredicate(String predicate) {
        Predicate = predicate;
    }

    public String getObject() {
        return Object;
    }

    public void setObject(String object) {
        Object = object;
    }

    public Boolean getCorrectness() {
        return correctness;
    }

    public void setCorrectness(Boolean correctness) {
        this.correctness = correctness;
    }

    @Override
    public String toString() {
        return this.getSubject()+" "+this.getPredicate()+" "+this.getObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRDF simpleRDF = (SimpleRDF) o;
        return Objects.equals(getId(), simpleRDF.getId()) &&
                Objects.equals(getSubject(), simpleRDF.getSubject()) &&
                Objects.equals(getPredicate(), simpleRDF.getPredicate()) &&
                Objects.equals(getObject(), simpleRDF.getObject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSubject(), getPredicate(), getObject());
    }
}

