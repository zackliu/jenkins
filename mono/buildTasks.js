'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.buildBuildTasks = buildBuildTasks;
exports.buildDevelopTasks = buildDevelopTasks;
exports.buildServeTasks = buildServeTasks;
exports.buildDeployThemeTasks = buildDeployThemeTasks;
exports.buildDeployLocThemeTasks = buildDeployLocThemeTasks;
exports.buildDeploySiteTasks = buildDeploySiteTasks;
function mergeTasks(taskList, plugins, command) {
  var result = taskList;
  var starts = plugins.get(command, 'start');
  if (starts && starts.length && starts.length > 0) {
    result = starts.concat(taskList);
  }
  var ends = plugins.get(command, 'end');
  if (ends && ends.length && ends.length > 0) {
    result = taskList.concat(ends);
  }

  return result;
}

function buildBuildTasks(project) {
  var taskList = void 0;
  if (project.passthrough) {
    taskList = ['clean', 'sprites', 'buildLib', 'postcss', 'webpack'];
  } else {
    taskList = ['clean', 'sprites', 'postcss', 'webpack'];
    if (project.stage === 'build') {
      taskList.push('copy:docpacker');
      //taskList.push('version');
    }
  }

  if (project.config.uxservice) {
    taskList.push('buildUXService');
  }

  return mergeTasks(taskList, project.plugins, 'build');
}

function buildDevelopTasks(project) {
  var taskList = buildBuildTasks(project);
  taskList = taskList.concat(['docfx', 'docpacker', 'watch']);
  return mergeTasks(taskList, project.plugins, 'develop');
}

function buildServeTasks(project) {
  var taskList = buildBuildTasks(project);
  taskList = taskList.concat(['docfx', 'docpacker', 'watch']);
  return mergeTasks(taskList, project.plugins, 'serve');
}

function buildDeployThemeTasks(project) {
  project.stage = 'build';
  var taskList = buildBuildTasks(project);
  taskList = taskList.concat(['deployTheme']);
  return mergeTasks(taskList, project.plugins, 'deployTheme');
}

function buildDeployLocThemeTasks(project) {
  project.stage = 'build';
  var taskList = buildBuildTasks(project);
  taskList = taskList.concat(['deployLocTheme']);
  return mergeTasks(taskList, project.plugins, 'deployLocTheme');
}

function buildDeploySiteTasks(project) {
  var taskList = buildBuildTasks(project);
  taskList = taskList.concat(['docfx', 'docpacker', 'copy:hosting-config', 'deploySite']);
  return mergeTasks(taskList, project.plugins, 'deploySite');
}