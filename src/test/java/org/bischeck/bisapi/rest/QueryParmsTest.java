package org.bischeck.bisapi.rest;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class QueryParmsTest {

  @Test(groups = { "unit" })
  public void getFields() {
	QueryParms qp = new QueryParms(Optional.ofNullable("state"),Optional.ofNullable(null));
	Assert.assertEquals(qp.getFilters().contains("state"),true);
	Assert.assertEquals(qp.getQueries().isEmpty(), true);
	qp = new QueryParms(Optional.ofNullable("state,value"),Optional.ofNullable(null));
	Assert.assertEquals(qp.getFilters().contains("state"),true);
	Assert.assertEquals(qp.getFilters().contains("value"),true);
	Assert.assertEquals(qp.getFilters().size(),2);
	Assert.assertEquals(qp.getQueries().isEmpty(), true);
  }

  @Test(groups = { "unit" })
  public void getQueries() {
	  QueryParms qp = new QueryParms(Optional.ofNullable(null),Optional.ofNullable("value>\"OK\"&&value==10"));
		Assert.assertEquals(qp.getFilters().isEmpty(),true);
		Assert.assertEquals(qp.getQueries().isEmpty(), false);
		Assert.assertEquals(qp.getQueries().contains("value>\"OK\"&&value==10"),true);
		Assert.assertEquals(qp.getFilters().size(),0);
		Assert.assertEquals(qp.getQueries().size(),1);
		Assert.assertEquals(qp.getFilters().isEmpty(), true);
  }

  @Test(groups = { "unit" })
  public void hasFilters() {
	  QueryParms qp = new QueryParms(Optional.ofNullable("state"),Optional.ofNullable(null));
	  Assert.assertEquals(qp.hasFilters(),true);
	  Assert.assertEquals(qp.hasQuery(),false);
  }

  @Test(groups = { "unit" })
  public void hasQuery() {
	  QueryParms qp = new QueryParms(Optional.ofNullable(null),Optional.ofNullable("value>\"OK\"&&value==10"));
	  Assert.assertEquals(qp.hasFilters(),false);
	  Assert.assertEquals(qp.hasQuery(),true);
  }
}
