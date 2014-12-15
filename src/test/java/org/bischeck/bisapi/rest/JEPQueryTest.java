package org.bischeck.bisapi.rest;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JEPQueryTest {

	@Test(groups = { "unit" })
	public void execute() {
		JEPQuery jepq = new JEPQuery("state==\"OK\"&&value>1000");
		Assert.assertEquals(jepq.getVariables().contains("state"), true);
		Assert.assertEquals(jepq.getVariables().contains("value"), true);
		Map<String, String> map = new HashMap<>();
		map.put("state", "\"OK\"");
		map.put("value", "1001");
		try {
			Assert.assertTrue(jepq.execute(map));
		} catch (IllegalAccessException e) {
			Assert.fail("IlleagalAccessExecption");
		}
		map.put("state", "\"OK\"");
		map.put("value", "1000");
		try {
			Assert.assertFalse(jepq.execute(map));
		} catch (IllegalAccessException e) {
			Assert.fail("IlleagalAccessExecption");
		}
	}

	@Test(groups = { "unit" })
	public void getVariables() {
		JEPQuery jepq = new JEPQuery("state==\"OK\"&&value>1000");
		Assert.assertEquals(jepq.getVariables().contains("state"), true);
		Assert.assertEquals(jepq.getVariables().contains("value"), true);
		Assert.assertEquals(jepq.getVariables().contains("nothing"), false);
	}

	@Test(groups = { "unit" })
	public void toStringT() {
		JEPQuery jepq = new JEPQuery("state==\"OK\"&&value>1000");
		Assert.assertEquals(jepq.toString(), "state==\"OK\"&&value>1000");
	}
}
