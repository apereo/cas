<a name="CASDocumentation">  </a>
# CAS Documentation

<a name="Requirements">  </a>
## Requirements
Strictly speaking, the only tools required to craft documentation are the standard development tools. You'll need [jekyll](http://jekyllrb.com/) to generate the site for preview purposes, but it's not strictly required.  
If you encounter the following error:

```ruby
runner.rb:365:in 'require_program': program version required (Commander::Runner::CommandError)
...
custom_require.rb:36:in 'gem_original_require': no such file to load -- json (LoadError)
```

It means that the json gem is missing: `gem install json` should solve the problem...

<a name="GettingStarted">  </a>
## Getting Started

1. `git clone git@github.com:Jasig/cas.git cas.site`
2. `git checkout gh-pages`
3. Change documentation as necessary
4. `git push --set-upstream origin gh-pages`

The changes should be almost immediately available at http://jasig.github.io/cas/.

<a name="LocalSiteGeneration">  </a>
## Local Site Generation
Generating the site to preview changes _before_ commit is encouraged. Install [jekyll](http://jekyllrb.com/) and execute the following command from the documentation root directory:

```bash
jekyll build --safe
```

Browse to the output directory `_site` in a browser to preview the generated site.

Alternatively, you may also invoke the documentation build script:

```bash
build.[bat|sh]
```

The build will auto-generate tags for section headers.

<a name="Troubleshooting">  </a>
## Troubleshooting
If you have trouble getting jekyll to build, try the following:

* Make sure python is included in your $PATH
* If you are receiving unicode incompatibility errors, try the command `chcp 65001`
* There exists a bug with Jekyll 1.4.3 that does not properly utilize file separators. You may have to downgrade to Jekyll 1.4.2 or upgrade to the next 2.x release.
