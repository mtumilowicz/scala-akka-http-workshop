package app.domain.cash

import app.domain.cash.error.NumberShouldBeNonNegativeError
import app.domain.error.DomainError

case class NonNegativeAmount private(raw: BigInt) extends AnyVal {
  def +(other: NonNegativeAmount): NonNegativeAmount =
    new NonNegativeAmount(raw + other.raw)

  def -(other: NonNegativeAmount): Either[DomainError, NonNegativeAmount] =
    NonNegativeAmount(raw - other.raw)
}

object NonNegativeAmount {
  def apply(bigInt: BigInt): Either[DomainError, NonNegativeAmount] =
    Either.cond(bigInt >= 0, new NonNegativeAmount(bigInt), NumberShouldBeNonNegativeError)

  def ZERO: NonNegativeAmount =
    new NonNegativeAmount(0)
}
