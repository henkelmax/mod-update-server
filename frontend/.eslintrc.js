module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: ['plugin:vue/essential', '@vue/airbnb'],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-use-before-define': 'off',
    'global-require': 'off',
    'arrow-parens': 'off',
    'no-multi-assign': 'off',
    'linebreak-style': 'off',
    'no-unused-vars': 'off',
    'object-shorthand': 'off',
    'max-len': 'off',
    'no-restricted-syntax': 'off',
    'operator-linebreak': 'off',
    'implicit-arrow-linebreak': 'off',
    'no-plusplus': [
      'error',
      {
        allowForLoopAfterthoughts: true
      }
    ],
    quotes: ['off', 'single'],
    'comma-dangle': ['error', 'never'],
    'no-underscore-dangle': 'off'
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
};
