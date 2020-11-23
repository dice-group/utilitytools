package org.dice.utilitytools.Model;

public class RDFProcessEntity {
  private String Subject;
  private String Predicate;
  private String Object;
  private String PreviusObject;
  private String Type;
  private Boolean HasTruthValue;
  private int RedyForProcess;
  private Boolean IsProcessed;
  private Boolean AfterProcessResultIsAcceptable;
  private Boolean DoesItChange;

  public RDFProcessEntity() {
    this.IsProcessed = false;
    this.AfterProcessResultIsAcceptable = false;
    this.RedyForProcess = 0;
    this.DoesItChange = false;
    this.PreviusObject = "";
  }

  public String getSubject() {
    return Subject;
  }

  public void AddStep() {
    this.RedyForProcess += 1;
  }

  public void setSubject(String subject) {
    Subject = subject;
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
    this.PreviusObject = this.Object;
    Object = object;
  }

  public Boolean getHasTruthValue() {
    return HasTruthValue;
  }

  public String getStringFormHasTruthValue() {
    if (HasTruthValue) {
      return "1.0";
    } else {
      return "0.0";
    }
  }

  public void setHasTruthValue(Boolean hasTruthValue) {
    HasTruthValue = hasTruthValue;
  }

  public int getRedyForProcess() {
    return RedyForProcess;
  }

  public void setRedyForProcess(int redyForProcess) {
    RedyForProcess = redyForProcess;
  }

  public Boolean getIsProcessed() {
    return IsProcessed;
  }

  public void setIsProcessed(Boolean isProcessed) {
    IsProcessed = isProcessed;
  }

  public Boolean getAfterProcessResultIsAcceptable() {
    return AfterProcessResultIsAcceptable;
  }

  public void setAfterProcessResultIsAcceptable(Boolean afterProcessResultIsAcceptable) {
    AfterProcessResultIsAcceptable = afterProcessResultIsAcceptable;
  }

  public Boolean getDoesItChange() {
    return DoesItChange;
  }

  public void setDoesItChange(Boolean doesItChange) {
    DoesItChange = doesItChange;
  }

  public String getPreviusObject() {
    return PreviusObject;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((HasTruthValue == null) ? 0 : HasTruthValue.hashCode());
    result = prime * result + ((IsProcessed == null) ? 0 : IsProcessed.hashCode());
    result = prime * result + ((Object == null) ? 0 : Object.hashCode());
    result = prime * result + ((Predicate == null) ? 0 : Predicate.hashCode());
    result = prime * result + RedyForProcess;
    result = prime * result + ((Subject == null) ? 0 : Subject.hashCode());
    return result;
  }

  @Override
  public boolean equals(java.lang.Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RDFProcessEntity other = (RDFProcessEntity) obj;
    if (HasTruthValue == null) {
      if (other.HasTruthValue != null) return false;
    } else if (!HasTruthValue.equals(other.HasTruthValue)) return false;
    if (IsProcessed == null) {
      if (other.IsProcessed != null) return false;
    } else if (!IsProcessed.equals(other.IsProcessed)) return false;
    if (Object == null) {
      if (other.Object != null) return false;
    } else if (!Object.equals(other.Object)) return false;
    if (Predicate == null) {
      if (other.Predicate != null) return false;
    } else if (!Predicate.equals(other.Predicate)) return false;
    if (RedyForProcess != other.RedyForProcess) return false;
    if (Subject == null) {
      if (other.Subject != null) return false;
    } else if (!Subject.equals(other.Subject)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "RDFProcessEntity [Subject="
        + Subject
        + ", Predicate="
        + Predicate
        + ", Object="
        + Object
        + ", HasTruthValue="
        + HasTruthValue
        + ", RedyForProcess="
        + RedyForProcess
        + ", IsProcessed="
        + IsProcessed
        + ", AfterProcessResultIsAcceptable="
        + AfterProcessResultIsAcceptable
        + "]";
  }
}
