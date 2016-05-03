'use strict';

var gulp = require('gulp'),
    sass = require('gulp-sass');

var BASE_DIR = './src/main/resources/static';
var SRC_DIR = BASE_DIR + '/sass/**/*.scss';
var DEST_DIR = BASE_DIR + '/css';


gulp.task('default', ['sass']);

gulp.task('sass', function () {
    return gulp.src(SRC_DIR)
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest(DEST_DIR));
});

gulp.task('sass:watch', function () {
    gulp.watch(SRC_DIR, ['sass']);
});
