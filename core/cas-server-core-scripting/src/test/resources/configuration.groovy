profiles {
    dev {
        cas.authn.accept.users="test::dev"
    }
    test {
        cas.authn.accept.users="test::alone"
    }
}

cas."service-registry".core."init-from-json"=true
cas.authn.accept.name="Static"
