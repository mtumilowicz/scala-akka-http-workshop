package app.domain.cash

case class NonNegativeAmount(raw: Int) {
  require(raw >= 0)

  def +(other: NonNegativeAmount): NonNegativeAmount =
    NonNegativeAmount(raw + other.raw)

  def -(other: NonNegativeAmount): NonNegativeAmount =
    NonNegativeAmount(raw - other.raw)
}
