package hochgi.repro.datatypes

sealed trait ManagedError
final case class NotFound(message: String) extends ManagedError
final case class UserError(msg: String) extends ManagedError
final case class NoAuth(msg: String) extends ManagedError
final case class Forbidden(msg: String) extends ManagedError
final case class NotImplemented(msg: String) extends ManagedError
final case class Unknown(code: Int, msg: String) extends ManagedError
