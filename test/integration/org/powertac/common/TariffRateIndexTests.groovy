package org.powertac.common

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant

class TariffRateIndexTests extends GroovyTestCase
{

  def timeService // dependency injection
  def sessionFactory

  TariffSpecification tariffSpec // instance var

  Instant start
  Instant exp
  Broker broker

  protected void setUp ()
  {
    super.setUp()
    start = new DateTime(2011, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).toInstant()
    timeService.setCurrentTime(start)
    broker = new Broker(username: 'testBroker', password: 'testPassword')
    assert broker.save()
    exp = new DateTime(2011, 3, 1, 12, 0, 0, 0, DateTimeZone.UTC).toInstant()
    tariffSpec = new TariffSpecification(broker: broker, expiration: exp,
        minDuration: TimeService.WEEK * 8)
  }

  protected void tearDown ()
  {
    super.tearDown()
  }

  void testPresistTariffWithRate ()
  {
    Rate r1 = new Rate(value: 0.121)
    tariffSpec.addToRates(r1)

    assert tariffSpec.save()
    Tariff te = new Tariff(tariffSpec: tariffSpec)
    te.init()

    if (te.rateMap) {
      println "rateMap is not null"
      for (row in 0..te.rateMap.length - 1) {
        for (col in 0..te.rateMap[row].length - 1) {
          println "rateMap[${row}][${col}] = ${te.rateMap[row][col]?.value}"
        }

      }
    }

    assert te.save()

    assertNotNull("non-null result", te)
    assertEquals("correct TariffSpec", tariffSpec, te.tariffSpec)
    assertEquals("correct initial realized price", 0.0, te.realizedPrice)
    assertEquals("correct expiration in spec", exp, te.tariffSpec.getExpiration())
    assertEquals("correct expiration", exp, te.getExpiration())
    assertEquals("correct publication time", start, te.offerDate)
    assertFalse("not expired", te.isExpired())
    assertTrue("covered", te.isCovered())

    // clear session
    sessionFactory.currentSession.clear()
    Tariff tariff = Tariff.get(te.id)
    assertNotNull(tariff)
    assertNotNull(tariff.rateMap)
    assertEquals("# of rows", te.rateMap.length, tariff.rateMap.length)
    assertEquals("# of columns", te.rateMap[0].length, tariff.rateMap[0].length)

    for (row in 0..tariff.rateMap.length - 1) {
      for (col in 0..tariff.rateMap[row].length - 1) {
        assertEquals("rateMap[${row}][${col}.value", te.rateMap[row][col].value, tariff.rateMap[row][col].value)
      }
    }
  }
}
