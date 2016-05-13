'use strict';

var gulp = require('gulp'),
    sass = require('gulp-sass');

var argv = require('yargs')
    .option('sassPath', {
        alias: 's',
        describe: "where's your sass?",
        default: './src/main/resources/static/sass'
    })
    .option('npmPath', {
        alias: 'n',
        describe: "where's your npm?",
        default: './node_modules'
    })
    .option('cssPath', {
        alias: 'c',
        describe: "where do you want your css?",
        default: './src/main/resources/static/css'
    })
    .help('help')
    .argv;

gulp.task('default', ['sass']);

gulp.task('sass', function () {
    process.stdout.write("running with these values:\n");
    process.stdout.write("\tsassPath: " + argv.sassPath + "\n");
    process.stdout.write("\tnpmPath: " + argv.npmPath + "\n");
    process.stdout.write("\tcssPath: " + argv.cssPath + "\n");
    process.stdout.write("\there: " + argv.here + "\n");
    return gulp.src(argv.sassPath + '/**/*.scss')
        .pipe(sass({
            //outputStyle: 'compressed',
            includePaths: [
                argv.sassPath,
                argv.npmPath + '/bootstrap-sass/assets/stylesheets'
            ]
        }).on('error', sass.logError))
        .pipe(gulp.dest(argv.cssPath));

});

gulp.task('sass:watch', function () {
    gulp.watch(argv.sassPath + '/**/*.scss', ['sass']);
});
