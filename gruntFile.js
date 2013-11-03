module.exports = function (grunt) {
    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-watch');
 
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        typescript: {
            base: {
                src: ['app/assets/javascripts/**/*.ts'],
                dest: 'public/javascripts/all.js',
                options: {
                    module: 'amd',
                    target: 'es5'
                }
            }
        },
		 watch: {
            files: '**/*.ts',
            tasks: ['typescript']
        }
    });
 
    grunt.registerTask('default', ['watch']);
 
}