package org.powertac.common.command

import org.joda.time.DateTime
import org.powertac.common.enumerations.BuySellIndicator
import org.powertac.common.enumerations.ModReasonCode
import org.powertac.common.enumerations.OrderType
import org.powertac.common.enumerations.ProductType
import org.powertac.common.*

class ShoutDoDeleteCmdTests extends GroovyTestCase {

  def timeService
  Product product
  Timeslot timeslot
  Broker broker
  Broker broker2
  Shout shout
  String userName
  String apiKey

  protected void setUp() {
    super.setUp()
    timeService.base = new DateTime().millis
    timeService.start = new DateTime().millis
    timeService.rate = 720l
    timeService.modulo = 1800000l
    timeService.updateTime()

    userName = 'testBroker'
    apiKey = 'testApiKey-which-needs-to-be-longer-than-32-characters'
    broker = new Broker(userName: userName, apiKey: apiKey)
    assert (broker.validate() && broker.save())
    broker2 = new Broker(userName: userName+'2', apiKey: apiKey+'2')
    assert (broker2.validate() && broker2.save())
    product = new Product(productType: ProductType.Future)
    assert (product.validate() && product.save())
    timeslot = new Timeslot(serialNumber: 0, startDateTime: new DateTime(), endDateTime: new DateTime())
    assert (timeslot.validate() && timeslot.save())
    shout = new Shout(product: product, timeslot: timeslot, broker: broker, quantity: 1.0, limitPrice: 10.0, buySellIndicator: BuySellIndicator.BUY, orderType: OrderType.LIMIT, transactionId: 'testTransaction', latest: true, shoutId: 'testShoutId')
    if (!shout.validate()) println shout.errors.allErrors
    assert (shout.validate() && shout.save())
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testNullableValidationLogic() {
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd()
    cmd.id = null
    assertFalse(cmd.validate())
    assertEquals('nullable', cmd.errors.getFieldError('id').getCode())
    assertEquals('nullable', cmd.errors.getFieldError('broker').getCode())
    assertEquals('nullable', cmd.errors.getFieldError('shout').getCode())
  }

  void testDeletedShout() {
    shout.modReasonCode = ModReasonCode.DELETIONBYSYSTEM
    shout.save()
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd(shout: shout, broker: broker)
    assertFalse(cmd.validate())
    assertEquals(Constants.SHOUT_DELETED, cmd.errors.getFieldError('shout').getCode())

    shout.modReasonCode = ModReasonCode.DELETIONBYUSER
    shout.save()
    ShoutDoDeleteCmd cmd1 = new ShoutDoDeleteCmd(shout: shout, broker: broker)
    assertFalse(cmd1.validate())
    assertEquals(Constants.SHOUT_DELETED, cmd1.errors.getFieldError('shout').getCode())
  }

  void testExecutedShout() {
    shout.modReasonCode = ModReasonCode.EXECUTION
    shout.save()
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd(shout: shout, broker: broker)
    assertFalse(cmd.validate())
    assertEquals(Constants.SHOUT_EXECUTED, cmd.errors.getFieldError('shout').getCode())
  }

  void testShoutOutdated(){
    shout.latest = false
    shout.save()
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd(shout: shout, broker: broker)
    assertFalse(cmd.validate())
    assertEquals(Constants.SHOUT_OUTDATED, cmd.errors.getFieldError('shout').getCode())
  }

  void testInvalidBroker(){
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd(shout: shout, broker: broker2)
    assertFalse(cmd.validate())
    assertEquals(Constants.SHOUT_WRONG_BROKER, cmd.errors.getFieldError('broker').getCode())
  }

  void testValidShoutDoDeleteCmd() {
    shout.modReasonCode = ModReasonCode.INSERT
    shout.save()
    ShoutDoDeleteCmd cmd = new ShoutDoDeleteCmd(shout: shout, broker: broker)
    assertTrue(cmd.validate())
  }
}
