package app.core

import app.domain.error.DomainError
import cats.data.EitherT

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import org.scalatest.EitherValues._

object GetAwaitResultSyntax {
  implicit class GetAwaitResultOps[T](value: EitherT[Future, DomainError, T]) {
    def success(implicit duration: Duration): T =
      GetAwaitResult.getSuccess(value)
    def failure(implicit duration: Duration): DomainError =
      GetAwaitResult.getFailure(value)
  }
}
