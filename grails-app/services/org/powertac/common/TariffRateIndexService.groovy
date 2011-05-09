package org.powertac.common

import java.util.Map.Entry

class TariffRateIndexService
{

  static transactional = true

  def create (tariffId, rateMap)
  {
    for (rowIdx in 0..rateMap.length - 1) {
      for (colIdx in 0..rateMap[rowIdx].length - 1) {
        if (rateMap[rowIdx][colIdx]) {
          println "TariffRateIndexService - [${rowIdx}][${colIdx}] = ${rateMap[rowIdx][colIdx].value}"
          println "TariffRateIndexService - [${rowIdx}][${colIdx}] = ${rateMap[rowIdx][colIdx].class.name}"
          TariffRateIndex.withNewSession {
            def tri = new TariffRateIndex(tariffId: tariffId, rowIdx: rowIdx, colIdx: colIdx, rateId: rateMap[rowIdx][colIdx].id)
            tri.save(flush: true)
            println tri
            if (tri.hasErrors()) {
              println "TariffRateIndexService ${tri}"
              tri.errors.allErrors.each { println it }
            } else {
              println "no error in saving"
            }
          }
        }
      }
    }
  }

  def load (tariffId)
  {
    def rateMap
    def indices = TariffRateIndex.findAllByTariffId(tariffId)
    if (indices) {
      def maxRowIdx = indices.collect { it.rowIdx }.max()
      def maxColIdx = indices.collect { it.colIdx }.max()

      rateMap = new Rate[maxRowIdx + 1][maxColIdx + 1]
      for (row in 0..maxRowIdx) {
        rateMap[row] = new Rate[maxColIdx + 1]
      }

      for (def index in indices) {
        def rate = Rate.get(index.rateId)
        if (rate) {
          rateMap[index.rowIdx][index.colIdx] = rate
        }
      }
    }
    rateMap
  }
}
