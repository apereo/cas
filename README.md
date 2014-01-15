# CAS Documentation

## Requirements
Strictly speaking, the only tools required to craft documentation are the standard development tools. You'll need [jekyll](http://jekyllrb.com/) to generate the site for preview purposes, but it's not strictly required.  
If you encounter the following error:

```ruby
runner.rb:365:in 'require_program': program version required (Commander::Runner::CommandError)
...
custom_require.rb:36:in 'gem_original_require': no such file to load -- json (LoadError)
```

It means that the json gem is missing: `gem install json` should solve the problem...

## Getting Started

1. `git clone git@github.com:Jasig/cas.git cas.site`
2. `git checkout gh-pages`
3. Change documentation as necessary
4. `git push --set-upstream origin gh-pages`

The changes should be almost immediately available at http://jasig.github.io/cas/.

## Local Site Generation
Generating the site to preview changes _before_ commit is encouraged. Install [jekyll](http://jekyllrb.com/) and execute the following command from the documentation root directory:

```bash
jekyll build -d /path/to/output/directory
```

Browse to the output directory, e.g. file:///path/to/output/directory, in a browser to preview the generated site.