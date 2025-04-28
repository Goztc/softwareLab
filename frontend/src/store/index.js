import { createStore } from 'vuex';

// 创建 store
const store = createStore({
    state: {
        toolbar: {
            enter: false,
            visible: false,
        },
        visible: {
            avatar: false,
            info: false,
            resetPassword: false,
            segwindows: false,
            wenxin: false,
        }
    },
    mutations: {
        changeToolbarStatus(state, toolbarStatus) {
            state.toolbar.enter = toolbarStatus.enter;
            state.toolbar.visible = toolbarStatus.visible;
        },
        changeVisibility(state, visibility) {
            state.visible = { ...state.visible, ...visibility };
        },
    },
    actions: {
        // 可以定义异步操作
    },
    getters: {
        // 可以定义派生状态
    },
});

export default store;
