'use strict';
var gulp = require('gulp');

var webserver = require('gulp-webserver'),
    serverport = 9502,
    watch = require('gulp-watch'),
    minifycss = require('gulp-clean-css'),
    gulpif = require('gulp-if'),
    uglify = require('gulp-uglify-es').default,
    concat = require('gulp-concat'),
    imagemin = require('gulp-imagemin'),
    rimraf = require('gulp-rimraf'),
    compass = require('gulp-compass'),
    cachebust = require('gulp-cache-bust'),
    inject = require('gulp-inject'),
    mainBowerFiles = require('main-bower-files'),
    runSequence = require('run-sequence'),
    path = require('path'),
    historyApiFallback = require('connect-history-api-fallback');;

//Common tasks
gulp.task('translate', function() {
    return gulp.src('src/app/translate/*')
               .pipe(gulp.dest('./dist/translate'));
});

gulp.task('fonts', function() {
    return gulp.src('src/app/fonts/*')
               .pipe(gulp.dest('./dist/fonts'));
});

gulp.task('image', function() {
    return gulp.src('src/app/images/**/*')
               .pipe(imagemin({ optimizationLevel: 3, progressive: true, interlaced: true }))
               .pipe(gulp.dest('./dist/images'));
});

gulp.task('compass', function() {
    return gulp.src('src/app/sass/*.scss')
               .pipe(compass({
                   config_file: 'config.rb',
                   css: 'src/app/styles',
                   sass: 'src/app/sass'
               }))
               .pipe(gulp.dest('./dist/styles'));
});

gulp.task('clean', function() {
    return gulp.src(['./dist/*'], { read: false })
               .pipe(rimraf());
});

gulp.task('copy-html', function() {
    return gulp.src('src/app/**/*.html')
        .pipe(gulp.dest('./dist'));
});

gulp.task('copy-bower-files', function() {
    return gulp.src(mainBowerFiles())
        .pipe(gulp.dest('./dist/assets/vendor'));
});

gulp.task('copy-plugin-files', function() {
    return gulp.src('src/app/plugins/**/*')
        .pipe(gulp.dest('./dist/assets/plugins'));
});

gulp.task('copy-app-files', function() {
    return gulp.src('src/app/scripts/**/*.js')
        .pipe(concat('app.js'))
        .pipe(gulp.dest('./dist/scripts'));
});

// Build local
gulp.task('html-inject', function() {
    return gulp.src('src/app/index.html')
            .pipe(inject(gulp.src('./dist/assets/plugins/**/*', { read: false }), {
                relative: false,
                transform: function (filePath, file) {
                    if ('.css' === path.extname(filePath)) {
                        return '<link href="' + filePath.replace('/dist', '') + '" rel="stylesheet" />' 
                    }
                    if ('.js' === path.extname(filePath)) {
                        return '<script src="' + filePath.replace('/dist', '') + '" type="text/javascript"></script>'
                    }
                    return ''
                }
            }))
            .pipe(inject(gulp.src(mainBowerFiles(), { read: false }), {
                starttag: '<!-- bower:{{ext}} -->',
                endtag: '<!-- endbower -->',
                relative: false,
                transform: function (filePath, file) {
                    if ('.css' === path.extname(filePath)) {
                        return '<link href="/assets/vendor/' + path.basename(filePath) + '" rel="stylesheet" />'
                    }
                    if ('.js' === path.extname(filePath)) {
                        return '<script src="/assets/vendor/' + path.basename(filePath) + '" type="text/javascript"></script>'
                    }
                    return ''
                }
            }))
            .pipe(inject(gulp.src('./dist/scripts/app.js', { read: false }), {
                starttag: '<!-- inject:app:{{ext}} -->',
                relative: false,
                transform: function (filePath, file) {
                    return '<script src="' + filePath.replace('/dist', '') + '" type="text/javascript"></script>'
                }
            }))
            .pipe(gulpif('*.css', minifycss()))
            .pipe(cachebust({
                type: 'timestamp'
            }))
            .pipe(gulp.dest('./dist'));
});

gulp.task('build-html', function() {
    runSequence('copy-html', 'html-inject');
});

gulp.task('html', function() {
    runSequence('copy-bower-files', 'copy-plugin-files', 'copy-app-files', 'build-html');
});

gulp.task('build',function() {
    runSequence('clean',
        ['image', 'translate', 'fonts', 'compass'],
        'html');
});

gulp.task('watch', ['build'], function() {
    gulp.watch(['src/app/images/**/*', 'src/app/**/*.html', 'src/app/**/*.js', 'src/app/styles/*.css'], ['build']);
    gulp.watch('src/app/sass/**/*', ['compass']);
});

gulp.task('server', ['watch'], function() {
    gulp.src('./dist/')
        .pipe(webserver({
            directoryListing: false,
            open: false,
            port: serverport,
            livereload: true,
            middleware: [ historyApiFallback() ]
      }));
});

// Build Production
gulp.task('html-inject-production', function() {
    return gulp.src('src/app/index.html')
            .pipe(inject(gulp.src('./dist/assets/plugins/**/*', { read: false }), {
                relative: false,
                transform: function (filePath, file) {
                    if ('.css' === path.extname(filePath)) {
                        return '<link href="' + filePath.replace('/dist', '') + '" rel="stylesheet" />' 
                    }
                    if ('.js' === path.extname(filePath)) {
                        return '<script src="' + filePath.replace('/dist', '') + '" type="text/javascript"></script>'
                    }
                    return ''
                }
            }))
            .pipe(inject(gulp.src(mainBowerFiles(), { read: false }), {
                starttag: '<!-- bower:{{ext}} -->',
                endtag: '<!-- endbower -->',
                relative: false,
                transform: function (filePath, file) {
                    if ('.css' === path.extname(filePath)) {
                        return '<link href="/assets/vendor/' + path.basename(filePath) + '" rel="stylesheet" />'
                    }
                    if ('.js' === path.extname(filePath)) {
                        return '<script src="/assets/vendor/' + path.basename(filePath) + '" type="text/javascript"></script>'
                    }
                    return ''
                }
            }))
            .pipe(inject(gulp.src('./dist/scripts/app.js', { read: false }), {
                relative: false,
                transform: function (filePath, file) {
                    return '<script src="' + filePath.replace('/dist', '') + '" type="text/javascript"></script>'
                }
            }))
            .pipe(gulpif('*.js', uglify({ mangle: false })))
            .pipe(gulpif('*.css', minifycss()))
            .pipe(cachebust({
                type: 'timestamp'
            }))
            .pipe(gulp.dest('./dist'));
});

gulp.task('build-html-production', function() {
    runSequence('copy-html' , 'html-inject-production');
});

gulp.task('html-production', function() {
    runSequence('copy-bower-files', 'copy-plugin-files', 'copy-app-files', 'build-html-production');
});

gulp.task('build-production', function() {
    runSequence('clean',
        ['image', 'translate', 'fonts', 'compass'],
        'html-production');
});
