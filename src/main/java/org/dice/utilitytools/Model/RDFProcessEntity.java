package org.dice.utilitytools.Model;

public class RDFProcessEntity {
  private String subject;
  private String predicate;
  private String previousPredicate;
  private String object;
  private String previousObject;
  private String type;
  private Boolean hasTruthValue;
  private int readyForProcess;
  private Boolean isProcessed;
  private Boolean afterProcessResultIsAcceptable;
  private Boolean doesItChange;
  private Boolean doesThePredicateChange;

  public RDFProcessEntity() {
    this.isProcessed = false;
    this.afterProcessResultIsAcceptable = false;
    this.readyForProcess = 0;
    this.doesItChange = false;
    this.previousObject = "";
    this.doesThePredicateChange = false;

  }

  public String getSubject() {
    return subject;
  }

  public void AddStep() {
    this.readyForProcess += 1;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getPredicate() {
    return predicate;
  }

  public void setPredicate(String predicate) {
    this.previousPredicate = this.predicate;
    this.predicate = predicate;
  }

  public String getPreviousPredicate() {
    return previousPredicate;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.previousObject = this.object;
    this.object = object;
  }

  public Boolean getHasTruthValue() {
    return hasTruthValue;
  }

  public String getStringFormHasTruthValue() {
    if (hasTruthValue) {
      return "1.0";
    } else {
      return "0.0";
    }
  }

  public void setHasTruthValue(Boolean hasTruthValue) {
    this.hasTruthValue = hasTruthValue;
  }

  public int getReadyForProcess() {
    return readyForProcess;
  }

  public void setReadyForProcess(int readyForProcess) {
    this.readyForProcess = readyForProcess;
  }

  public Boolean getIsProcessed() {
    return isProcessed;
  }

  public void setIsProcessed(Boolean isProcessed) {
    this.isProcessed = isProcessed;
  }

  public Boolean getAfterProcessResultIsAcceptable() {
    return afterProcessResultIsAcceptable;
  }

  public void setAfterProcessResultIsAcceptable(Boolean afterProcessResultIsAcceptable) {
    this.afterProcessResultIsAcceptable = afterProcessResultIsAcceptable;
  }

  public Boolean getDoesItChange() {
    return doesItChange;
  }

  public void setDoesItChange(Boolean doesItChange) {
    this.doesItChange = doesItChange;
  }

  public String getPreviousObject() {
    return previousObject;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getDoesThePredicateChange() {
    return doesThePredicateChange;
  }

  public void setDoesThePredicateChange(Boolean doesThePredicateChange) {
    this.doesThePredicateChange = doesThePredicateChange;
  }

  public void switchObjectAndSubject() {
    String temp  = subject;
    subject = object;
    object = temp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RDFProcessEntity that = (RDFProcessEntity) o;

    if (getReadyForProcess() != that.getReadyForProcess()) return false;
    if (getSubject() != null ? !getSubject().equals(that.getSubject()) : that.getSubject() != null) return false;
    if (getPredicate() != null ? !getPredicate().equals(that.getPredicate()) : that.getPredicate() != null)
      return false;
    if (getObject() != null ? !getObject().equals(that.getObject()) : that.getObject() != null) return false;
    if (getPreviousObject() != null ? !getPreviousObject().equals(that.getPreviousObject()) : that.getPreviousObject() != null)
      return false;
    if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
    if (getHasTruthValue() != null ? !getHasTruthValue().equals(that.getHasTruthValue()) : that.getHasTruthValue() != null)
      return false;
    if (getIsProcessed() != null ? !getIsProcessed().equals(that.getIsProcessed()) : that.getIsProcessed() != null)
      return false;
    if (getAfterProcessResultIsAcceptable() != null ? !getAfterProcessResultIsAcceptable().equals(that.getAfterProcessResultIsAcceptable()) : that.getAfterProcessResultIsAcceptable() != null)
      return false;
    if (getDoesItChange() != null ? !getDoesItChange().equals(that.getDoesItChange()) : that.getDoesItChange() != null)
      return false;
    return getDoesThePredicateChange() != null ? getDoesThePredicateChange().equals(that.getDoesThePredicateChange()) : that.getDoesThePredicateChange() == null;
  }

  @Override
  public int hashCode() {
    int result = getSubject() != null ? getSubject().hashCode() : 0;
    result = 31 * result + (getPredicate() != null ? getPredicate().hashCode() : 0);
    result = 31 * result + (getObject() != null ? getObject().hashCode() : 0);
    result = 31 * result + (getPreviousObject() != null ? getPreviousObject().hashCode() : 0);
    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
    result = 31 * result + (getHasTruthValue() != null ? getHasTruthValue().hashCode() : 0);
    result = 31 * result + getReadyForProcess();
    result = 31 * result + (getIsProcessed() != null ? getIsProcessed().hashCode() : 0);
    result = 31 * result + (getAfterProcessResultIsAcceptable() != null ? getAfterProcessResultIsAcceptable().hashCode() : 0);
    result = 31 * result + (getDoesItChange() != null ? getDoesItChange().hashCode() : 0);
    result = 31 * result + (getDoesThePredicateChange() != null ? getDoesThePredicateChange().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("RDFProcessEntity [Subject=");
    sb.append(subject);
    sb.append(", Predicate=");
    sb.append(predicate);
    sb.append(", Object=");
    sb.append(object);
    sb.append(", previusObject");
    sb.append(previousObject);
    sb.append(", HasTruthValue=");
    sb.append(hasTruthValue);
    sb.append(", RedyForProcess=");
    sb.append(readyForProcess);
    sb.append(", IsProcessed=");
    sb.append(isProcessed);
    sb.append(", AfterProcessResultIsAcceptable=");
    sb.append(afterProcessResultIsAcceptable);
    sb.append(", doesThePredicateChange=");
    sb.append(doesThePredicateChange);
    sb.append(", getPreviousPredicate=");
    sb.append(previousPredicate);
    sb.append("]");

    return sb.toString();
  }


}
