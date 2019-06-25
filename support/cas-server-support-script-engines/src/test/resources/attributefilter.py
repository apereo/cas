def run(*Params):
  Attributes = Params[0]
  Logger = Params[1]
  Logger.debug("Current attributes are {}", Attributes)
  return Attributes
