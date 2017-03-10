'use strict';

var gulp = require('gulp'),
    sass = require('gulp-sass'),
    autoprefixer = require('gulp-autoprefixer');

var argv = require('yargs')
    .option('sassPath', {
        alias: 's',
        describe: "where's your sass?",
        default: './resources/static/sass'
    })
    .option('npmPath', {
        alias: 'n',
        describe: "where's your npm?",
        default: './node_modules'
    })
    .option('cssPath', {
        alias: 'c',
        describe: "where do you want your css?",
        default: './resources/static/css'
    })
    .help('help')
    .argv;

var sassOptions = {
    errLogToConsole: true,
    outputStyle: 'expanded',
    includePaths: [
        argv.sassPath,
        argv.npmPath + '/bootstrap-sass/assets/stylesheets'
    ]
};

var autoprefixerOptions = {
    browsers: ['last 2 versions', '> 5%', 'Firefox ESR']
};


gulp.task('default', ['sass']);

gulp.task('sass', function () {
    process.stdout.write("running with these values:\n");
    process.stdout.write("\tsassPath: " + argv.sassPath + "\n");
    process.stdout.write("\tnpmPath: " + argv.npmPath + "\n");
    process.stdout.write("\tcssPath: " + argv.cssPath + "\n");
    return gulp
        .src(argv.sassPath + '/**/*.scss')
        .pipe(sass(sassOptions).on('error', sass.logError))
        .pipe(autoprefixer(autoprefixerOptions))
        .pipe(gulp.dest(argv.cssPath));

});

gulp.task('sass:watch', function () {
    gulp.watch(argv.sassPath + '/**/*.scss', ['sass']);
});
