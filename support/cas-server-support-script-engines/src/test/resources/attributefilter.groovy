import java.util.*

def run(final Object... args) {
  def currentAttributes = args[0]
  def logger = args[1]

  logger.debug("Current attributes received are {}", currentAttributes)
  return currentAttributes
}
