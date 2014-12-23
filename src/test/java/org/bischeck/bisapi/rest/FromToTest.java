package org.bischeck.bisapi.rest;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit" })
public class FromToTest {

	@Test(groups = { "unit" })
	public void offset() {
		FromTo fromto = new FromTo(Optional.ofNullable("5"),
				Optional.ofNullable("15"));
		Assert.assertEquals(fromto.hasOffset(), new Long("11"));
		fromto = new FromTo(Optional.ofNullable("0"), Optional.ofNullable("15"));
		Assert.assertEquals(fromto.hasOffset(), new Long("16"));
		fromto = new FromTo(Optional.ofNullable("0"),
				Optional.ofNullable("+15"));
		Assert.assertEquals(fromto.hasOffset(), new Long("15"));
		fromto = new FromTo(Optional.ofNullable("0"),
				Optional.ofNullable("  +15	"));
		Assert.assertEquals(fromto.hasOffset(), new Long("15"));
		fromto = new FromTo(Optional.ofNullable(" 0   "),
				Optional.ofNullable("  +15	"));
		Assert.assertEquals(fromto.hasOffset(), new Long("15"));
		fromto = new FromTo(Optional.ofNullable(" 10   "),
				Optional.ofNullable("  +5	"));
		Assert.assertEquals(fromto.hasOffset(), new Long("5"));
		Assert.assertEquals(fromto.getTo(), new Long("14"));
		Assert.assertEquals(fromto.getFrom(), new Long("10"));

		fromto = new FromTo(5, 15);
		Assert.assertEquals(fromto.hasOffset(), new Long("11"));
		Assert.assertEquals(fromto.getTo(), new Long("15"));
		Assert.assertEquals(fromto.getFrom(), new Long("5"));
	}
}
