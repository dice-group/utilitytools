package org.dice.utilitytools.service.Query;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueryExecutioner {

  @Value("${sparql.url.default}")
  private String serviceRequestURL;

  public QueryExecutioner() {}

  public QueryExecutioner(String serviceRequestURL) {
    this.serviceRequestURL = serviceRequestURL;
  }

/*  public QueryExecutioner(String serviceRequestURL) {
    this.serviceRequestURL = serviceRequestURL;
  }

  public void setServiceRequestURL(String serviceRequestURL) {
    this.serviceRequestURL = serviceRequestURL;
  }*/

  public QueryExecution getQueryExecution(Query query) {
    return QueryExecutionFactory.createServiceRequest(serviceRequestURL, query);
  }

  public QueryExecution getQueryExecution(String queryStr) {
    Query q = QueryFactory.create(queryStr);
    return getQueryExecution(q);
  }
}
