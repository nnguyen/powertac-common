package org.powertac.common

class TariffRateIndex implements Serializable {
  int rowIdx
  int colIdx
  String rateId
  String tariffId

  static constraints = {
    
  }

  static mapping = {
    id composite: ['rowIdx', 'colIdx', 'tariffId']
  }

  String toString() { "rowIdx:${rowIdx},colIdx:${colIdx},rateId:${rateId},tariffId:${tariffId}" }
}
