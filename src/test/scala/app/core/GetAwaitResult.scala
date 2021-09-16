package app.core

import app.domain.error.DomainError
import cats.data.EitherT

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import org.scalatest.EitherValues._

trait GetAwaitResult[T] {

  def getSuccess(future: EitherT[Future, DomainError, T])(implicit duration: Duration): T
  def getFailure(future: EitherT[Future, DomainError, T])(implicit duration: Duration): DomainError

}

object GetAwaitResult {
  def getSuccess[T](future: EitherT[Future, DomainError, T])(implicit duration: Duration): T = {
    Await.result(future.value, duration).value
  }

  def getFailure[T](future: EitherT[Future, DomainError, T])(implicit duration: Duration): DomainError = {
    Await.result(future.value, duration).left.value
  }
}
