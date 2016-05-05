'use strict';

var gulp = require('gulp'),
    sass = require('gulp-sass');

var config = {
    sassPath: './src/main/resources/static/sass',
    npmPath: './node_modules',
    cssPath: './src/main/resources/static/css'
}

gulp.task('default', ['sass']);

gulp.task('sass', function () {
    return gulp.src(config.sassPath + '/**/*.scss')
        .pipe(sass({
            //outputStyle: 'compressed',
            includePaths: [
                config.sassPath,
                config.npmPath + '/bootstrap-sass/assets/stylesheets'
            ]
        }).on('error', sass.logError))
        .pipe(gulp.dest(config.cssPath));

});

gulp.task('sass:watch', function () {
    gulp.watch(config.sassPath + '/**/*.scss', ['sass']);
});
