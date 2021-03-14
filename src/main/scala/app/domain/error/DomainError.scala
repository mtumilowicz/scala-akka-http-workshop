package app.domain.error

trait DomainError {
  def message(): String
}
