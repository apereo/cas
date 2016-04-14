'use strict';

var BASE_DIR = './src/main/webapp/';
var DEST = BASE_DIR;
var SRC = BASE_DIR;

var gulp = require('gulp'),
    sass = require('gulp-sass'),
    livereload = require('gulp-livereload');

//gulp.task('less', function() {
//    gulp.src( SRC + 'less/**/*.less' )
//        .pipe(less())
//        .pipe(gulp.dest( DEST + 'css' ))
//        .pipe(livereload());
//});
//
//gulp.task('watch', function() {
//    livereload.listen();
//    gulp.watch( SRC + 'less/**/*.less', ['less'] );
//});

//gulp.task('default', ['watch']);



gulp.task('sass', function () {
    return gulp.src('./sass/**/*.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest('./css'));
});

gulp.task('sass:watch', function () {
    gulp.watch('./sass/**/*.scss', ['sass']);
});


