/*
 * Druid - a distributed column store.
 * Copyright (C) 2012  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.metamx.druid.realtime.plumber;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.druid.QueryGranularity;
import com.metamx.druid.aggregation.AggregatorFactory;
import com.metamx.druid.aggregation.CountAggregatorFactory;
import com.metamx.druid.index.v1.SpatialDimensionSchema;
import com.metamx.druid.input.InputRow;
import com.metamx.druid.realtime.FireHydrant;
import com.metamx.druid.realtime.Schema;
import com.metamx.druid.shard.NoneShardSpec;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.util.List;

/**
 */
public class SinkTest
{
  @Test
  public void testSwap() throws Exception
  {
    final Schema schema = new Schema(
        "test",
        Lists.<SpatialDimensionSchema>newArrayList(),
        new AggregatorFactory[]{new CountAggregatorFactory("rows")},
        QueryGranularity.MINUTE,
        new NoneShardSpec()
    );

    final Interval interval = new Interval("2013-01-01/2013-01-02");
    final String version = new DateTime().toString();
    final Sink sink = new Sink(interval, schema, version);

    sink.add(
        new InputRow()
        {
          @Override
          public List<String> getDimensions()
          {
            return Lists.newArrayList();
          }

          @Override
          public long getTimestampFromEpoch()
          {
            return new DateTime("2013-01-01").getMillis();
          }

          @Override
          public List<String> getDimension(String dimension)
          {
            return Lists.newArrayList();
          }

          @Override
          public float getFloatMetric(String metric)
          {
            return 0;
          }

          @Override
          public Object getRaw(String dimension)
          {
            return null;
          }
        }
    );

    FireHydrant currHydrant = sink.getCurrIndex();
    Assert.assertEquals(new Interval("2013-01-01/PT1M"), currHydrant.getIndex().getInterval());


    FireHydrant swapHydrant = sink.swap();

    sink.add(
        new InputRow()
        {
          @Override
          public List<String> getDimensions()
          {
            return Lists.newArrayList();
          }

          @Override
          public long getTimestampFromEpoch()
          {
            return new DateTime("2013-01-01").getMillis();
          }

          @Override
          public List<String> getDimension(String dimension)
          {
            return Lists.newArrayList();
          }

          @Override
          public float getFloatMetric(String metric)
          {
            return 0;
          }

          @Override
          public Object getRaw(String dimension)
          {
            return null;
          }
        }
    );

    Assert.assertEquals(currHydrant, swapHydrant);
    Assert.assertNotSame(currHydrant, sink.getCurrIndex());
    Assert.assertEquals(new Interval("2013-01-01/PT1M"), sink.getCurrIndex().getIndex().getInterval());

    Assert.assertEquals(2, Iterators.size(sink.iterator()));
  }
}
