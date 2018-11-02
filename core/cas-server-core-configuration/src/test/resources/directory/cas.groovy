profiles {
    dev {
        cas.authn.accept.users="test::dev"
    }
    test {
        cas.authn.accept.users="test::alone"
    }
}

cas.authn.accept.name="Static"
