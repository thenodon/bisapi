package org.bischeck.bisapi.rest;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = { "unit" })
public class FromToTestIT {

	@Test(groups = { "unit" })
	public void offset() {
		FromTo fromto = new FromTo(Optional.ofNullable("5"),
				Optional.ofNullable("15"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("11"));
		fromto = new FromTo(Optional.ofNullable("0"), Optional.ofNullable("15"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("16"));
		fromto = new FromTo(Optional.ofNullable("0"),
				Optional.ofNullable("+15"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("15"));
		fromto = new FromTo(Optional.ofNullable("0"),
				Optional.ofNullable("  +15	"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("15"));
		fromto = new FromTo(Optional.ofNullable(" 0   "),
				Optional.ofNullable("  +15	"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("15"));
		fromto = new FromTo(Optional.ofNullable(" 10   "),
				Optional.ofNullable("  +5	"));
		Assert.assertEquals(fromto.hasOffset(), new Integer("5"));
		Assert.assertEquals(fromto.getTo(), new Integer("14"));
		Assert.assertEquals(fromto.getFrom(), new Integer("10"));

		fromto = new FromTo(5, 15);
		Assert.assertEquals(fromto.hasOffset(), new Integer("11"));
		Assert.assertEquals(fromto.getTo(), new Integer("15"));
		Assert.assertEquals(fromto.getFrom(), new Integer("5"));
	}
}
