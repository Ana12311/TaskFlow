// @vitest-environment jsdom
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { defineComponent, h } from 'vue'
import Login from '../Login.vue'

// 桩组件：只渲染必要的 DOM，不引入 element-plus 真实实现，避免 jsdom 缺浏览器 API
const ElInput = defineComponent({
  name: 'ElInput',
  props: ['placeholder', 'type'],
  setup(props) {
    return () => h('input', { placeholder: props.placeholder, type: props.type || 'text' })
  },
})
const passthrough = (name, tag) =>
  defineComponent({
    name,
    setup(_, { slots }) {
      return () => h(tag, slots.default ? slots.default() : [])
    },
  })

function mountLogin() {
  const router = createRouter({ history: createMemoryHistory(), routes: [] })
  return mount(Login, {
    global: {
      plugins: [router, createPinia()],
      // RouterLink 由 router 插件自动注册，这里只补 element-plus 的桩
      components: {
        ElCard: passthrough('ElCard', 'div'),
        ElForm: passthrough('ElForm', 'form'),
        ElFormItem: passthrough('ElFormItem', 'div'),
        ElButton: passthrough('ElButton', 'button'),
        ElInput,
      },
    },
  })
}

describe('Login.vue', () => {
  it('渲染品牌、登录按钮、注册入口和两个输入框', () => {
    const wrapper = mountLogin()

    expect(wrapper.text()).toContain('TaskFlow')
    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('去注册')
    expect(wrapper.findAll('input').length).toBe(2)
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
  })
})
