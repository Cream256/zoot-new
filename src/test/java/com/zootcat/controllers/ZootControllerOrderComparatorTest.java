package com.zootcat.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zootcat.controllers.Controller;
import com.zootcat.controllers.ControllerPriority;
import com.zootcat.controllers.ZootControllerOrderComparator;

public class ZootControllerOrderComparatorTest
{	
	@Mock private Controller ctrl1;
	@Mock private Controller ctrl2;
	@Mock private Controller ctrl3;
	@Mock private Controller ctrl4;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		when(ctrl1.getPriority()).thenReturn(ControllerPriority.High);
		when(ctrl2.getPriority()).thenReturn(ControllerPriority.Normal);
		when(ctrl3.getPriority()).thenReturn(ControllerPriority.Low);
		when(ctrl4.getPriority()).thenReturn(null);
	}
	
	@Test
	public void shouldProvideDescedingOrder()
	{
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl1, ctrl2) < 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl2, ctrl3) < 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl1, ctrl3) < 0);		
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl2, ctrl1) > 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl3, ctrl2) > 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl3, ctrl1) > 0);
	}
	
	@Test
	public void shouldProvideEqualityWhenComparingTheSamePriority()
	{
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl1, ctrl1) == 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl2, ctrl2) == 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl3, ctrl3) == 0);
	}
	
	@Test
	public void shouldUseDefaultPriorityWhenControllerHasNone()
	{
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl2, ctrl4) == 0);
		assertTrue(ZootControllerOrderComparator.Instance.compare(ctrl4, ctrl2) == 0);		
	}
}
