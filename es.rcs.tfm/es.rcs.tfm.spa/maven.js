var gulp = require('gulp');
var gutil = require('gulp-util');
var argv = require('yargs').argv;
var del = require('del');
var mkdirp = require('mkdirp');
var touch = require('touch');

function stringToBoolean(string){
  if(!string) {
    return false;
  }
  switch(string.toLowerCase()) {
    case "false": return false;
    case "no": return false;
    case "0": return false;
    case "": return false;
    case "true": return true;
    default:
      throw new Error("unknown string: " + string);
  }
}

argv.skipTests = stringToBoolean(argv.skipTests);

/* TASKS */

gulp.task('clean', function(cb){
  del(['./build'], cb);
});


gulp.task( 'build', function () {
  mkdirp.sync('./build');
  touch.sync('./build/index.html');
} );


gulp.task( 'test', function () {
  if(argv.skipTests) {
    gutil.log(gutil.colors.yellow('Skipping Tests'));
    return;
  }

    gutil.log('Running Tests');
} );

gulp.task('prepare-for-maven-war', function(){
  return gulp.src('./build/**').pipe(gulp.dest('target/gulp'));
});